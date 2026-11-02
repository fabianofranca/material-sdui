package com.fabianofranca.sdui.contract

/**
 * Os componentes ligados diretamente a este: os filhos e os que ficam dentro das props
 * (ícones da TopAppBar, `leading`/`trailing` do ListItem, botões do Dialog).
 */
fun Component.nestedComponents(): List<Component> = when (this) {
    is Component.TopAppBar -> listOfNotNull(props.navigationIcon) + props.actions
    is Component.ListItem -> listOfNotNull(props.leading, props.trailing)
    is Component.Dialog -> listOfNotNull(props.confirm, props.dismiss)
    else -> children
}

/** Este componente e todos os que estão dentro dele, em profundidade. */
fun Component.descendantsAndSelf(): Sequence<Component> = sequence {
    yield(this@descendantsAndSelf)
    for (child in nestedComponents()) yieldAll(child.descendantsAndSelf())
}

/** As raízes da tela, na ordem: `topBar`, `content`, `bottomBar`, `floating` e `modals`. */
val Screen.roots: List<Component>
    get() = listOfNotNull(topBar, content, bottomBar, floating) + modals

/** Todos os componentes da tela. */
fun Screen.allComponents(): Sequence<Component> = roots.asSequence().flatMap { it.descendantsAndSelf() }

/** Ids que aparecem mais de uma vez na tela (devem ser únicos). */
fun Screen.duplicateIds(): Set<String> =
    allComponents().map { it.id }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

/** Tipos desconhecidos na tela (a versão do app é mais velha que a do componente). */
fun Screen.unknownTypes(): Set<String> =
    allComponents().filterIsInstance<Component.Unknown>().map { it.type }.toSet()
