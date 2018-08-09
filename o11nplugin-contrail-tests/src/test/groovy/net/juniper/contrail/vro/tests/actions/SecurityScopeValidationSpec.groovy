/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.api.types.PolicyManagement
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

    def project1DraftPolicyManagement = dependencies.someProjectDraftPolicyManagement(project1)
    def project2DraftPolicyManagement = dependencies.someProjectDraftPolicyManagement(project2)
    def globalDraftPolicyManagement = dependencies.globalDraftPolicyManagement

    def project1FirewallRule = dependencies.someProjectFirewallRule(project1)
    def globalFirewallRule = dependencies.someGlobalFirewallRule()
    def project1DraftFirewallRule = dependencies.someDraftFirewallRule(project1DraftPolicyManagement)
    def project2DraftFirewallRule = dependencies.someDraftFirewallRule(project2DraftPolicyManagement)
    def globalDraftFirewallRule = dependencies.someDraftFirewallRule(globalDraftPolicyManagement)

    def project1ServiceGroup = dependencies.someProjectServiceGroup(project1)
    def project2ServiceGroup = dependencies.someProjectServiceGroup(project2)
    def globalServiceGroup = dependencies.someGlobalServiceGroup()
    def project1DraftServiceGroup = dependencies.someDraftServiceGroup(project1DraftPolicyManagement)
    def project2DraftServiceGroup = dependencies.someDraftServiceGroup(project2DraftPolicyManagement)
    def globalDraftServiceGroup = dependencies.someDraftServiceGroup(globalDraftPolicyManagement)

    def project1Tag = dependencies.someProjectTag(project1)
    def project2Tag = dependencies.someProjectTag(project2)
    def globalTag = dependencies.someGlobalTag()

    def mockDraftPolicyManagements() {
        connectorMock.findByFQN(PolicyManagement.class, project1DraftPolicyManagement.qualifiedName.join(":")) >> project1DraftPolicyManagement.__getTarget()
        connectorMock.findByFQN(PolicyManagement.class, project2DraftPolicyManagement.qualifiedName.join(":")) >> project2DraftPolicyManagement.__getTarget()
        connectorMock.findByFQN(PolicyManagement.class, globalDraftPolicyManagement.qualifiedName.join(":")) >> globalDraftPolicyManagement.__getTarget()
    }

    def "Validating a null object when creating a project-scope firewall rule" () {
        def children = null
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // Service Group, rule creation
    def "Validating a same-project-scope Service Group when creating a project-scope firewall rule" () {
        given:
        def children = project1ServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Service Group when creating a project-scope firewall rule" () {
        given:
        def children = project2ServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when creating a project-scope firewall rule" () {
        given:
        def children = globalServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Service Group when creating a global-scope firewall rule" () {
        given:
        def children = project1ServiceGroup
        def parent = connection
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when creating a global-scope firewall rule" () {
        given:
        def children = globalServiceGroup
        def parent = connection
        def arrayMode = false
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // Tags, rule creation
    def "Validating a same-project-scope Tag when creating a project-scope firewall rule" () {
        given:
        def children = [project1Tag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Tag when creating a project-scope firewall rule" () {
        given:
        def children = [project2Tag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when creating a project-scope firewall rule" () {
        given:
        def children = [globalTag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Tag when creating a global-scope firewall rule" () {
        given:
        def children = [project1Tag]
        def parent = connection
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when creating a global-scope firewall rule" () {
        given:
        def children = [globalTag]
        def parent = connection
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a list of various Tags, one of which is wrong, when creating a project-scope firewall rule" () {
        given:
        def children = [project1Tag, project2Tag, globalTag]
        def parent = project1
        def arrayMode = true
        def directMode = true

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails, naming the wrong tag"
        validationFailureWith(result, securityScopeValidationMessage(project2Tag.name))
    }


    // Service Group, rule edition
    def "Validating a same-project-scope Service Group when editing a project-scope firewall rule" () {
        given:
        def children = project1ServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Service Group when editing a project-scope firewall rule" () {
        given:
        def children = project2ServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when editing a project-scope firewall rule" () {
        given:
        def children = globalServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Service Group when editing a global-scope firewall rule" () {
        given:
        def children = project1ServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Service Group when editing a global-scope firewall rule" () {
        given:
        def children = globalServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // Tags, rule edition
    def "Validating a same-project-scope Tag when editing a project-scope firewall rule" () {
        given:
        def children = [project1Tag]
        def parent = project1FirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a different-project-scope Tag when editing a project-scope firewall rule" () {
        given:
        def children = [project2Tag]
        def parent = project1FirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when editing a project-scope firewall rule" () {
        given:
        def children = [globalTag]
        def parent = project1FirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a project-scope Tag when editing a global-scope firewall rule" () {
        given:
        def children = [project1Tag]
        def parent = globalFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }
    def "Validating a global-scope Tag when editing a global-scope firewall rule" () {
        given:
        def children = [globalTag]
        def parent = globalFirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
    def "Validating a list of various Tags, one of which is wrong, when editing a project-scope firewall rule" () {
        given:
        def children = [project1Tag, project2Tag, globalTag]
        def parent = project1FirewallRule
        def arrayMode = true
        def directMode = false

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails, naming the wrong tag"
        validationFailureWith(result, securityScopeValidationMessage(project2Tag.name))
    }

    // draft mode
    // project-project, direct, same project

    def "project:draft child, project:non-draft parent, direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "project:non-draft child, project:draft parent, direct" () {
        given:
        def children = project1ServiceGroup
        def parent = project1DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "project:draft child, project:draft parent, direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project1DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // project-project, direct, different projects

    def "project:draft child, project:non-draft parent, direct, different projects" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project2
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:non-draft child, project:draft parent, direct, different projects" () {
        given:
        def children = project2ServiceGroup
        def parent = project1DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:draft child, project:draft parent, direct, different projects" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project2DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    // project-project, non-direct, same projects

    def "project:non-draft child, project:draft parent, non-direct" () {
        given:
        def children = project1ServiceGroup
        def parent = project1DraftFirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "project:draft child, project:non-draft parent, non-direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "project:draft child, project:draft parent, non-direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project1DraftFirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // project-project, non-direct, different projects

    def "project:non-draft child, project:draft parent, non-direct, different projects" () {
        given:
        def children = project1ServiceGroup
        def parent = project2DraftFirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:draft child, project:non-draft parent, non-direct, different projects" () {
        given:
        def children = project2DraftServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:draft child, project:draft parent, non-direct, different projects" () {
        given:
        def children = project1DraftServiceGroup
        def parent = project2DraftFirewallRule
        def arrayMode = false
        def directMode = false

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    // project-global, direct

    def "project:draft child, global:draft parent, direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = globalDraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:non-draft child, global:draft parent, direct" () {
        given:
        def children = project1ServiceGroup
        def parent = globalDraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:draft child, global:non-draft parent, direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = connection
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    // project-global, non-direct

    def "project:draft child, global:draft parent, non-direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = globalDraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:non-draft child, global:draft parent, non-direct" () {
        given:
        def children = project1ServiceGroup
        def parent = globalDraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    def "project:draft child, global:non-draft parent, non-direct" () {
        given:
        def children = project1DraftServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it fails"
        validationFailure(result)
    }

    // global-project, direct

    def "global:draft child, project:draft parent, direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = project1DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:non-draft child, project:draft parent, direct" () {
        given:
        def children = globalServiceGroup
        def parent = project1DraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:draft child, project:non-draft parent, direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = project1
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // global-project, non-direct

    def "global:draft child, project:draft parent, non-direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = project1DraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:non-draft child, project:draft parent, non-direct" () {
        given:
        def children = globalServiceGroup
        def parent = project1DraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:draft child, project:non-draft parent, non-direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = project1FirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // global-global, direct

    def "global:draft child, global:draft parent, direct" () {
        given:
        def children = globalDraftFirewallRule
        def parent = globalDraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:non-draft child, global:draft parent, direct" () {
        given:
        def children = globalFirewallRule
        def parent = globalDraftPolicyManagement
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:draft child, global:non-draft parent, direct" () {
        given:
        def children = globalDraftFirewallRule
        def parent = connection
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    // global-global, non-direct

    def "global:draft child, global:draft parent, non-direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = globalDraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:non-draft child, global:draft parent, non-direct" () {
        given:
        def children = globalServiceGroup
        def parent = globalDraftFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }

    def "global:draft child, global:non-draft parent, non-direct" () {
        given:
        def children = globalDraftServiceGroup
        def parent = globalFirewallRule
        def arrayMode = false
        def directMode = true

        mockDraftPolicyManagements()

        when: "executing validating script"
        def result = invokeFunction(validateSecurityScope, children, parent, directMode, arrayMode)

        then: "it succeeds"
        validationSuccess(result)
    }
}
