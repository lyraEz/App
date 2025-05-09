# Stretch Screen Android App

Este aplicativo simula o efeito de tela esticada usado pelos jogadores profissionais de CS:GO, aplicando-o a todo o sistema Android.

## Características
- Efeito de tela esticada sistema-wide
- Controles ajustáveis para largura e altura
- Presets predefinidos (4:3, 16:10, 5:4)
- Capacidade de salvar presets personalizados
- Inicialização automática no boot do dispositivo

## Requisitos
- Android 5.0 (API 21) ou superior
- Permissão "Sobrepor a outras apps" habilitada
- Acesso root recomendado para funcionalidade completa (mas não obrigatório)

## Como compilar
1. Extraia os arquivos deste pacote
2. Abra o projeto no Android Studio
3. Clique em "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
4. Instale o APK em seu dispositivo Android

## Como usar
1. Abra o aplicativo
2. Conceda a permissão "Sobrepor a outras apps" quando solicitado
3. Ative o efeito usando o switch na tela principal
4. Ajuste as configurações de largura e altura conforme desejado
5. Use os presets predefinidos ou crie seus próprios presets personalizados

## Notas
- As configurações de largura e altura são relativas (100% = normal)
- Valores maiores que 100% para largura fazem a tela mais larga
- Valores maiores que 100% para altura fazem a tela mais alta
- A eficácia do efeito de esticamento depende da versão do Android e do dispositivo
- Dispositivos com acesso root podem ter melhor desempenho

## Solução de problemas
- Se o efeito não aparecer, verifique se a permissão "Sobrepor a outras apps" está concedida
- Em alguns dispositivos, pode ser necessário reiniciar o aplicativo após conceder a permissão
- Se o efeito não funcionar como esperado, tente ajustar os valores de largura e altura para valores extremos para ver a diferença