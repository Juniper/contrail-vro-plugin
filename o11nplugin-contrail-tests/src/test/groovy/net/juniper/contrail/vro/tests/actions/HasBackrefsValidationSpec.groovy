package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.tests.workflows.WorkflowSpec

import static net.juniper.contrail.vro.config.Actions.hasBackrefs

class HasBackrefsValidationSpec extends WorkflowSpec implements ValidationAsserts{
    def hasBackrefsValidation = actionFromScript(hasBackrefs)
    def message = "Object is still referenced by other objects"

    def "null item results in null"() {
        given: "item is set to null"
            def item = null
        when: "executing validation script"
            def result = engine.invokeFunction(hasBackrefsValidation, item)
        then: "it return null"
            validationSuccess(result)
    }

    def "item with empty backref list results in null"() {
        given: "item is PolicySet without backrefs"
            def item = dependencies.someNetworkPolicy()
        when: "executing validation script"
            def result = engine.invokeFunction(hasBackrefsValidation, item)
        then: "it return null"
            validationSuccess(result)
    }

    def "item with not empty backref list result in message"() {
        given: "item.countBackrefs() returns anything other than 0"
                //we use mocked object because of limitations of objects provided to test
            def item = Mock(NetworkPolicy_Wrapper.class)
            item.countBackrefs() >> 1
        when: "executing validation script"
            def result = engine.invokeFunction(hasBackrefsValidation, item)
        then: "it return message"
            validationFailureWith(result, message)
    }
}