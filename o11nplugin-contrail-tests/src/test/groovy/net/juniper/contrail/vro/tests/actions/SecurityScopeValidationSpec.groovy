/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.vro.tests.workflows.WorkflowSpec

import static net.juniper.contrail.vro.config.Actions.matchesSecurityScope

// workflowSpec is required for dependencies
class SecurityScopeValidationSpec extends WorkflowSpec implements ValidationAsserts {
    def validateSecurityScope = actionFromScript(matchesSecurityScope)
    String securityScopeValidationMessage(badObjectName) {
        return "$badObjectName comes from an inaccessible project."
    }

    def connection = dependencies.connection
    def project1 = dependencies.someProject()
    def project2 = dependencies.someProject()
    def projectFirewallRule = dependencies.someProjectFirewallRule(project1)
    def globalFirewallRule = dependencies.someGlobalFirewallRule()

    def project1ServiceGroup = dependencies.someProjectServiceGroup(project1)
    def project2ServiceGroup = dependencies.someProjectServiceGroup(project2)
    def globalServiceGroup = dependencies.someGlobalServiceGroup()

    def project1Tag = dependencies.someProjectTag(project1)
    def project2Tag = dependencies.someProjectTag(project2)
    def globalTag = dependencies.someGlobalTag()

    // Service Group, rule creation
    def "Validating a same-project-scope Service Group when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project1ServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Service Group when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project2ServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = globalServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Service Group when creating a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project1ServiceGroup
        def parent = connection
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when creating a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = globalServiceGroup
        def parent = connection
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // Tags, rule creation
    def "Validating a same-project-scope Tag when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Tag when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project2Tag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [globalTag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Tag when creating a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag]
        def parent = connection
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when creating a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [globalTag]
        def parent = connection
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a list of various Tags, one of which is wrong, when creating a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag, project2Tag, globalTag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails, naming the wrong tag"
        validationFailureWith(result, securityScopeValidationMessage(project2Tag.name))
    }


    // Service Group, rule edition
    def "Validating a same-project-scope Service Group when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project1ServiceGroup
        def parent = projectFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Service Group when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project2ServiceGroup
        def parent = projectFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = globalServiceGroup
        def parent = projectFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Service Group when editing a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = project1ServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when editing a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = globalServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // Tags, rule edition
    def "Validating a same-project-scope Tag when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag]
        def parent = projectFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Tag when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project2Tag]
        def parent = projectFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [globalTag]
        def parent = projectFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Tag when editing a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag]
        def parent = globalFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when editing a global-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [globalTag]
        def parent = globalFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a list of various Tags, one of which is wrong, when editing a project-scope firewall rule" () {
        given: "aforementioned parameters"
        def children = [project1Tag, project2Tag, globalTag]
        def parent = projectFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = engine.invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails, naming the wrong tag"
        validationFailureWith(result, securityScopeValidationMessage(project2Tag.name))
    }
}
