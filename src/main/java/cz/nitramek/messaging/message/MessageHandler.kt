package cz.nitramek.messaging.message

abstract class MessageHandler {
    open fun handle(send: Send) {}

    open fun handle(ack: Ack) {}

    open fun handle(result: Result) {}

    open fun handle(store: Store) {}

    open fun handle(agents: Agents) {}

    open fun handle(addAgents: AddAgents) {}

    open fun handle(unknownMessage: UnknownMessage) {}

    open fun handle(aPackage: Package) {}
    open fun handle(execute: Execute) {}
    open fun handle(halt: Halt) {}

}