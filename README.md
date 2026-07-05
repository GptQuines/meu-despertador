# Despertador - Alarme Android com Flash Piscante

Aplicativo despertador Android nativo com interface moderna, 4 abas (Despertador, Rel\u00f3gio Mundial, Cron\u00f4metro, Temporizador) e funcionalidade exclusiva de flash piscando sincronizado com o alarme.

## Tecnologias

- **Kotlin** 100%
- **Jetpack Compose** (UI declarativa)
- **Material Design 3** (Material You com Dynamic Color)
- **MVVM** + Repository Pattern
- **Room Database** (persist\u00eancia local)
- **Hilt** (inje\u00e7\u00e3o de depend\u00eancia)
- **AlarmManager** (agendamento de alarmes exatos)
- **CameraManager** (controle do flash)
- **Navigation Compose** (navega\u00e7\u00e3o entre abas)

## Como compilar

### Requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- Android SDK 34

### Passos

1. Abra o Android Studio
2. Selecione **File > Open** e escolha a pasta do projeto
3. Aguarde o Gradle sincronizar
4. Execute **Build > Make Project** ou clique em **Run**

### Via linha de comando

`ash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
`

O APK ser\u00e1 gerado em:
- pp/build/outputs/apk/debug/app-debug.apk
- pp/build/outputs/apk/release/app-release.apk

## Instala\u00e7\u00e3o

1. Copie o APK para seu celular
2. No celular, abra o arquivo APK
3. Confirme a instala\u00e7\u00e3o (talvez precise ativar "Fontes desconhecidas")
4. Abra o aplicativo e conceda as permiss\u00f5es necess\u00e1rias

## Estrutura do projeto

`
com.despertador.app/
\u2514\u2500\u2500 ui/
    \u2514\u2500\u2500 main/           # MainActivity + BottomNavigation
    \u2514\u2500\u2500 alarm/          # Lista de alarmes
    \u2514\u2500\u2500 alarmdetail/    # Criar/editar alarme
    \u2514\u2500\u2500 alarmringing/   # Tela do alarme tocando
    \u2514\u2500\u2500 worldclock/     # Rel\u00f3gio Mundial
    \u2514\u2500\u2500 stopwatch/      # Cron\u00f4metro
    \u2514\u2500\u2500 timer/          # Temporizador
    \u2514\u2500\u2500 theme/          # Tema Material You
\u2514\u2500\u2500 data/
    \u2514\u2500\u2500 model/          # Entidade Alarm
    \u2514\u2500\u2500 db/             # Room Database + DAO
    \u2514\u2500\u2500 repository/     # AlarmRepository
\u2514\u2500\u2500 di/                # AppModule (Hilt)
\u2514\u2500\u2500 service/           # AlarmScheduler + AlarmRingingService
\u2514\u2500\u2500 receiver/          # BroadcastReceivers
`

## Funcionalidades

- **Alarme**: criar, editar, excluir, ativar/desativar
- **Repeti\u00e7\u00e3o**: dias da semana, dias \u00fateis, fins de semana, diariamente
- **Soneca**: configur\u00e1vel (5-30 min, m\u00e1ximo de sonecas)
- **Vibra\u00e7\u00e3o**: liga/desliga, cont\u00ednua enquanto toca
- **Flash**: pisca 300ms ligado / 300ms desligado durante o alarme
- **Som**: toque padr\u00e3o de alarme do Android (TYPE_ALARM)
- **Tela de bloqueio**: alarme aparece mesmo com tela bloqueada
- **Boot**: alarmes persistem ap\u00f3s reinicializa\u00e7\u00e3o

## Permiss\u00f5es

- SCHEDULE_EXACT_ALARM - alarmes exatos
- POST_NOTIFICATIONS - notifica\u00e7\u00f5es (Android 13+)
- CAMERA - controle do flash
- VIBRATE - vibra\u00e7\u00e3o
- RECEIVE_BOOT_COMPLETED - reagendamento ap\u00f3s boot
- FOREGROUND_SERVICE - servi\u00e7o em primeiro plano

## Gerar nova vers\u00e3o

1. Atualize ersionCode e ersionName em pp/build.gradle.kts
2. Execute ./gradlew assembleRelease
3. O APK assinado estar\u00e1 em pp/build/outputs/apk/release/app-release.apk

## Build autom\u00e1tico (GitHub Actions)

A cada push no branch main, o GitHub Actions compila automaticamente e gera o APK como artefato.
