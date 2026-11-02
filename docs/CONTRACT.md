# Contrato do JSON (v1)

A fonte única do contrato é o módulo [`contract`](../contract/src/commonMain/kotlin/com/fabianofranca/sdui/contract). Este documento resume o que está lá. Exemplos reais, gerados pelo BFF, ficam em [`docs/examples/`](examples).

## Resposta de uma tela

`GET {bff}/screens/{rota}?{parâmetros}` devolve:

```json
{
  "version": 1,
  "screen": {
    "id": "settings",
    "title": "Configurações",
    "state": { "notifications": true, "email": "" },
    "topBar": { "type": "TopAppBar", "id": "top", "props": { "title": "Configurações" } },
    "content": { "type": "Column", "id": "content", "children": [] },
    "bottomBar": null,
    "floating": null,
    "modals": [],
    "events": { "onAppear": { "type": "track", "name": "settings_view" } }
  }
}
```

- O app manda `X-SDUI-Version: 1` (a maior versão que entende). Se a versão for velha demais, o BFF responde `426`.
- O BFF manda `ETag` e `Cache-Control: no-cache`. O app guarda a última resposta de cada tela e manda `If-None-Match`; com `304`, usa o cache. Sem rede, também usa o cache.
- `content` é obrigatório. `topBar`, `bottomBar`, `floating` (o FAB) e `modals` (BottomSheet e Dialog) são opcionais.
- `events` da tela: `onAppear`, `onRefresh` (puxar para atualizar a tela) e `onBack` (voltar do sistema).

## Componente

```json
{
  "type": "Button",
  "id": "save",
  "props": { "label": "Salvar", "style": "filled", "enabled": { "$state": "formValid" } },
  "events": { "onClick": { "type": "navigateBack" } },
  "visible": true,
  "testTag": "profile.save",
  "a11y": { "label": "Salvar perfil", "heading": false },
  "analytics": { "name": "profile_save", "properties": {} }
}
```

| Campo       | O que é                                                                                    |
| ----------- | ------------------------------------------------------------------------------------------ |
| `type`      | O tipo (tabela abaixo).                                                                    |
| `id`        | Estável e único na tela. Chave de testes, analytics, campos, camadas e paginação.          |
| `props`     | As props do tipo. Todas opcionais, com padrão.                                             |
| `children`  | Só nos contêineres (`Column`, `Row`, `Box`, `LazyColumn`, `Card`, `Badge`, `BottomSheet`). |
| `events`    | Gatilho → ação. Uma lista de ações vira `sequence`.                                        |
| `visible`   | `true`, `false` ou uma ligação ao estado. Ligado a uma chave que não existe = invisível.   |
| `testTag`   | Vira `testTag` no Compose (e `resource-id` no Android, para UiAutomator/Appium/Maestro).   |
| `a11y`      | `label` (o `contentDescription`) e `heading` (título para leitores de tela).               |
| `analytics` | Com `name`, o componente gera impressão ao aparecer e clique ao ser tocado.                |

### Ligações ao estado

Qualquer prop marcada como _ligável_ aceita um valor fixo ou uma ligação:

- `{ "$state": "email" }`: o valor da chave `email`.
- `{ "$state": "aba", "equals": "noticias" }`: `true` se `aba` for `"noticias"`, senão `false`. Serve para `visible`, `enabled`, `checked`… (por exemplo, mostrar o conteúdo da aba selecionada). É uma comparação, não uma linguagem de expressões.

Ligações também valem dentro de `request.body`, `request.query`, `navigate.params`, `setState.value` e `track.properties`, em qualquer nível.

### Campos

`TextField.value`, `Checkbox.checked`, `Switch.checked`, `RadioGroup.selected`, `Chip.selected`, `Tabs.selected` e `NavigationBar.selected` guardam o valor do campo:

- ligados ao estado (`{ "$state": "email" }`), o que a pessoa faz é gravado nessa chave;
- com valor fixo, o campo guarda o valor no estado pela chave igual ao próprio `id` (e `request.body` pode ler `{ "$state": "<id>" }`).

Mudar o valor dispara `onChange` e limpa o erro do campo.

## Componentes (26)

Props _ligáveis_ estão com `*`. Tamanhos e espaços são em dp. Cores e tipografia são papéis do Material 3 (veja o fim da página).

| Tipo                | Props                                                                                                                                                                                                         | Gatilhos                             |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ |
| `TopAppBar`         | `title*`, `style` (`small`, `center`, `medium`, `large`), `navigationIcon` (componente), `actions` (componentes)                                                                                              | –                                    |
| `Column`            | `spacing`, `padding`, `horizontalAlignment` (`start`, `center`, `end`), `scroll`                                                                                                                              | `onClick`, `onAppear`                |
| `Row`               | `spacing`, `padding`, `verticalAlignment` (`top`, `center`, `bottom`), `horizontalArrangement` (`start`, `center`, `end`, `spaceBetween`, `spaceAround`, `spaceEvenly`), `wrap` (quebra a linha)              | `onClick`, `onAppear`                |
| `Box`               | `contentAlignment` (`topStart` … `bottomEnd`, `center`), `padding`                                                                                                                                            | `onClick`, `onAppear`                |
| `LazyColumn`        | `spacing`, `padding`                                                                                                                                                                                          | `onLoadMore`, `onRefresh`, `onAppear` |
| `Spacer`            | `size`                                                                                                                                                                                                        | –                                    |
| `Divider`           | `inset`                                                                                                                                                                                                       | –                                    |
| `Text`              | `text*`, `style` (tipografia), `color` (cor), `maxLines`, `align` (`start`, `center`, `end`, `justify`)                                                                                                        | `onClick`, `onAppear`                |
| `Image`             | `url*`, `contentScale` (`crop`, `fit`, `fillBounds`, `fillWidth`, `fillHeight`, `inside`, `none`), `aspectRatio`, `size` (quadrada), `placeholder` (ícone)                                                     | `onClick`, `onAppear`                |
| `Icon`              | `name`, `tint` (cor), `size`                                                                                                                                                                                  | `onClick`                            |
| `Card`              | `style` (`filled`, `elevated`, `outlined`), `padding` (padrão 16), `spacing` (padrão 8)                                                                                                                       | `onClick`, `onAppear`                |
| `ListItem`          | `headline*`, `supporting*`, `overline*`, `leading` (componente), `trailing` (componente)                                                                                                                      | `onClick`, `onAppear`                |
| `Badge`             | `count*` (sem ele, um ponto; ≤ 0, nada)                                                                                                                                                                       | –                                    |
| `ProgressIndicator` | `kind` (`linear`, `circular`), `progress*` (0 a 1; sem ele, indeterminado)                                                                                                                                    | –                                    |
| `Button`            | `label*`, `style` (`filled`, `tonal`, `elevated`, `outlined`, `text`), `icon`, `enabled*`, `loading*`, `fullWidth`                                                                                            | `onClick`, depois `onSubmit`          |
| `IconButton`        | `icon`, `style` (`standard`, `filled`, `tonal`, `outlined`), `enabled*`                                                                                                                                       | `onClick`                            |
| `FAB`               | `icon`, `label*` (com ele, estendido), `size` (`small`, `regular`, `large`)                                                                                                                                   | `onClick`                            |
| `TextField`         | `value*`, `label*`, `placeholder*`, `supportingText*`, `error*`, `keyboard` (`text`, `email`, `number`, `phone`, `password`), `imeAction` (`default`, `done`, `next`, `search`, `send`, `go`), `maxLength`, `style` (`filled`, `outlined`), `singleLine`, `enabled*`, `validation` | `onChange`, `onSubmit`               |
| `Checkbox`          | `checked*`, `label*`, `enabled*`, `validation` (só `required`)                                                                                                                                                | `onChange`                           |
| `Switch`            | `checked*`, `label*`, `enabled*`                                                                                                                                                                              | `onChange`                           |
| `RadioGroup`        | `options` (`value`, `label`, `enabled`), `selected*`, `enabled*`, `validation` (só `required`)                                                                                                                | `onChange`                           |
| `Chip`              | `label*`, `kind` (`assist`, `filter`, `input`, `suggestion`), `selected*`, `icon`, `enabled*`                                                                                                                 | `onClick`; `onChange` no `filter`    |
| `NavigationBar`     | `items` (`id`, `icon`, `label`, `route`, `badge`, `testTag`, `events`), `selected*`                                                                                                                           | `onChange`; `onClick` em cada item   |
| `Tabs`              | `items` (`id`, `label`, `icon`, `testTag`, `events`), `selected*`, `style` (`primary`, `secondary`)                                                                                                           | `onChange`; `onClick` em cada aba    |
| `BottomSheet`       | `dragHandle`                                                                                                                                                                                                  | `onDismiss`                          |
| `Dialog`            | `title*`, `text*`, `icon`, `confirm` (componente), `dismiss` (componente)                                                                                                                                     | `onDismiss`                          |

Detalhes:

- `Column`, `Row`, `LazyColumn`, `Card`, `TextField`, `Checkbox`/`Switch`/`RadioGroup` com rótulo e `ProgressIndicator` linear ocupam a largura disponível.
- `padding` é um número (os quatro lados) ou `{ "all", "horizontal", "vertical", "start", "top", "end", "bottom" }`.
- Item da `NavigationBar` sem `onClick` navega para `route` trocando a tela atual (`replace`).
- Tocar em `confirm` ou `dismiss` do `Dialog` fecha o Dialog e depois executa o `onClick` do botão.
- `onDismiss` só dispara quando a pessoa fecha a camada (arrastar, tocar fora, voltar), não com `closeSheet`/`closeDialog`.
- `onLoadMore` dispara perto do fim da lista, uma vez para cada tamanho de lista. Enquanto a ação roda (com `loading: component`), aparece um indicador no fim.
- O Snackbar não é declarado: aparece com a ação `showSnackbar`.

### Validação

`validation` é uma lista de regras; a ação `validate` mostra a mensagem da primeira que falhar.

| `type`      | `value`             | Passa quando                       |
| ----------- | ------------------- | ---------------------------------- |
| `required`  | –                   | o campo está preenchido ou marcado |
| `minLength` | o tamanho           | tem pelo menos `value` caracteres  |
| `maxLength` | o tamanho           | tem no máximo `value` caracteres   |
| `pattern`   | a expressão regular | casa com a expressão inteira       |
| `email`     | –                   | parece um e-mail                   |

Fora `required`, as regras não olham campo vazio (campo opcional vazio passa). Regra de `type` desconhecido passa sempre.

## Gatilhos

`onClick`, `onChange`, `onSubmit`, `onAppear`, `onRefresh`, `onLoadMore`, `onDismiss`, `onBack`. Ficam para depois: `onLongClick`, `onFocus`, `onBlur`.

## Ações

| Ação                                     | Parâmetros                                                                                                                                                                                 |
| ---------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `navigate`                               | `route`, `params` (ligáveis), `replace`                                                                                                                                                    |
| `navigateBack`                           | –                                                                                                                                                                                          |
| `openDeeplink`                           | `uri`                                                                                                                                                                                      |
| `openUrl`                                | `url`, `mode` (`external`, `inApp`)                                                                                                                                                        |
| `openSheet` / `closeSheet`               | `id` do BottomSheet (no `close`, opcional: sem ele, o último aberto)                                                                                                                       |
| `openDialog` / `closeDialog`             | `id` do Dialog (idem)                                                                                                                                                                      |
| `showSnackbar`                           | `message`, `actionLabel`, `action`, `duration` (`short`, `long`, `indefinite`), `onDismiss`                                                                                                |
| `request`                                | `method` (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), `endpoint` (relativo ao BFF ou absoluto), `query` (ligáveis), `body` (ligável em qualquer nível), `loading` (`none`, `screen`, `component`; padrão `component`), `onSuccess`, `onError` |
| `setState`                               | `key`, `value` (pode ser uma ligação, para copiar outra chave)                                                                                                                             |
| `validate`                               | `fields` (ids), `onValid`, `onInvalid`                                                                                                                                                     |
| `refreshScreen`                          | –                                                                                                                                                                                          |
| `track`                                  | `name`, `properties` (ligáveis)                                                                                                                                                            |
| `sequence`                               | `actions` (em ordem; cada uma termina antes da próxima)                                                                                                                                    |

Ficam para depois: `closeFlow`, `condition`, `share`, `copyToClipboard`, `call`, `requestPermission`, `openAppSettings`.

### Resposta de um `request`

Código 2xx executa `onSuccess`; qualquer outro código ou falha de rede executa `onError`. O corpo pode trazer, em sucesso ou em erro (tudo opcional):

```json
{
  "state": { "page": 3 },
  "errors": { "email": "Este e-mail já está em uso" },
  "append": { "feed": [ { "type": "Card", "id": "article-a11", "children": [] } ] },
  "action": { "type": "showSnackbar", "message": "Pronto" }
}
```

- `state`: mesclado ao estado da tela.
- `errors`: erros por id de campo, mostrados como os de `validate` (a validação do BFF).
- `append`: filhos acrescentados ao contêiner de cada id (a paginação da `LazyColumn`).
- `action`: executada antes do `onSuccess`.

Enquanto o `request` roda: com `loading: component`, o componente que disparou mostra carregamento (o `Button` mostra um indicador) e ignora toques repetidos; com `loading: screen`, a tela fica coberta.

Autenticação e cabeçalhos do app não ficam no JSON: o app os configura no `HttpClient` que entrega ao renderer.

## Compatibilidade

- Componente de `type` desconhecido não é desenhado (e é registrado em log).
- Ação de `type` desconhecido não faz nada (e é registrada).
- Gatilho desconhecido é descartado.
- Prop desconhecida é ignorada; valor de enum desconhecido cai no padrão da prop.
- Prop nova é sempre opcional. Uma prop fixa pode virar ligável sem quebrar nada (o valor fixo continua válido).
- Remover algo ou mudar o sentido de algo sobe a `version`.

## Papéis do Material 3

Cores (`color`, `tint`): `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`, `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`, `error`, `onError`, `errorContainer`, `onErrorContainer`, `background`, `onBackground`, `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`, `surfaceContainerLowest`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest`, `inverseSurface`, `inverseOnSurface`, `inversePrimary`, `outline`, `outlineVariant`.

Tipografia (`style` do `Text`): `displayLarge`, `displayMedium`, `displaySmall`, `headlineLarge`, `headlineMedium`, `headlineSmall`, `titleLarge`, `titleMedium`, `titleSmall`, `bodyLarge`, `bodyMedium`, `bodySmall`, `labelLarge`, `labelMedium`, `labelSmall`.

Ícones: nomes dos Material Symbols em snake_case (`arrow_back`, `settings`). O renderer já conhece os do `material-icons-core` ([lista](../renderer/src/commonMain/kotlin/com/fabianofranca/sdui/renderer/Icons.kt)); o app acrescenta os seus com `SduiIcons`.
