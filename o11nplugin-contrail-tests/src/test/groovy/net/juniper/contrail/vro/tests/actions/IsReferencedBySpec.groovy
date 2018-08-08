package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.tests.workflows.WorkflowSpec
import static net.juniper.contrail.vro.config.Actions.isReferencedBy

class IsReferencedBySpec extends WorkflowSpec implements ValidationAsserts{
    def isReferencedByAction = actionFromScript(isReferencedBy)
    def errorMessage = "Already referenced"

    def "null child results in success validation"() {
        given: "child is set to null"
        def parent = null
        def child = null
        when: "executing validation script"
        def result = engine.invokeFunction(isReferencedByAction, child, parent)
        then: "it succeeds"
        validationSuccess(result)
    }

    def "child which is not already referenced by parent results in success validation"() {
        given: "parent is set to some virtual network and child is set to some policy"
        def parent = dependencies.someVirtualNetwork()
        def child = dependencies.someNetworkPolicy()
        when: "executing validation script"
        def result = engine.invokeFunction(isReferencedByAction, child, parent)
        then: "it succeeds"
        validationSuccess(result)
    }

    def "child which is already referenced by parent results in failure validation with message"() {
        given: "parent is set to some virtual network and child is set to some policy"
        def parent = dependencies.someVirtualNetwork()
        def child = Mock(NetworkPolicy_Wrapper)
        child.isReferencedByVirtualNetwork(parent) >> true
        when: "executing validation script"
        def result = engine.invokeFunction(isReferencedByAction, child, parent)
        then: "it fails with message"
        validationFailureWith(result, errorMessage)
    }

}