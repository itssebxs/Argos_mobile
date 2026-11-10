import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Detecta cuando el usuario vuelve a la app desde un enlace de recuperación
/// de contraseña de Supabase.
///
/// Importante: [start] debe llamarse inmediatamente después de
/// `Supabase.initialize()`, antes de `runApp()`. El SDK de Supabase procesa el
/// deep link durante la inicialización y emite el evento `passwordRecovery`;
/// si escuchamos más tarde (por ejemplo dentro de un `initState`), el evento
/// puede perderse y el usuario nunca ve la pantalla de nueva contraseña.
class PasswordRecoveryHandler {
  PasswordRecoveryHandler._();

  /// `true` cuando hay una recuperación de contraseña pendiente de atender.
  static final ValueNotifier<bool> pending = ValueNotifier<bool>(false);

  static StreamSubscription<AuthState>? _subscription;
  static bool _started = false;

  static void start() {
    if (_started) return;
    _started = true;

    _subscription = Supabase.instance.client.auth.onAuthStateChange.listen(
      (data) {
        if (data.event == AuthChangeEvent.passwordRecovery) {
          pending.value = true;
        }
      },
      onError: (Object error, StackTrace stackTrace) {
        debugPrint('ARGOS: error en flujo de recuperación: $error');
      },
    );
  }

  /// Marca la recuperación como atendida.
  static void consume() => pending.value = false;

  static void dispose() {
    _subscription?.cancel();
    _subscription = null;
    _started = false;
    pending.value = false;
  }
}
