/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.api.Status
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import net.juniper.contrail.vro.model.ConnectionException
import spock.lang.Specification

class ConnectionSpec extends Specification {

    def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")
    def connector = Mock(ApiConnector)
    def connection = new Connection(info, connector)
    def someApiObject = Mock(ApiObjectBase)
    def someApiClass = VirtualNetwork.class // Class can't be mocked
    def someApiUri = "someString"
    def someObjectId = "someObjectId"
    def someObjectName = "objectName"
    def someObjectFQN = "someObjectFQN"
    def someAncestryList = ["ancestor1", "ancestor2", someObjectName]
    def someReferenceList = [Mock(ObjectReference), Mock(ObjectReference)]
    def success = Status.success()
    def failure = Status.failure("Not supported")

    def "calling create operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.create(someApiObject) >> { throw new IOException("sorry") }

        when: "trying to create some Contrail object"
        connection.create(someApiObject)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling read operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.read(someApiObject) >> { throw new IOException("sorry") }

        when: "trying to read some Contrail object"
        connection.read(someApiObject)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling update operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.update(someApiObject) >> { throw new IOException("sorry") }

        when: "trying to update some Contrail object"
        connection.update(someApiObject)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling delete operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.delete(someApiObject) >> { throw new IOException("sorry") }

        when: "trying to delete some Contrail object"
        connection.delete(someApiObject)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling second version of delete operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.delete(someApiClass, someObjectId) >> { throw new IOException("sorry") }

        when: "trying to delete some Contrail object"
        connection.delete(someApiClass, someObjectId)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling sync operation on connector throwing IOException" () {
        given: "a connector failing with IOException"
        connector.sync(someApiUri) >> { throw new IOException("sorry") }

        when: "trying to sync some Contrail object"
        connection.sync(someApiUri)

        then: "it fails with IOException"
        thrown IOException
    }

    def "calling create operation on connector returning error value" () {
        given: "a connector returning false"
        connector.create(someApiObject) >> failure

        when: "trying to create some Contrail object"
        connection.create(someApiObject)

        then: "it fails with ContrailApiException"
        thrown ConnectionException
    }

    def "calling read operation on connector returning error value" () {
        given: "a connector returning false"
        connector.read(someApiObject) >> failure

        when: "trying to read some Contrail object"
        connection.read(someApiObject)

        then: "it fails with ContrailApiException"
        thrown ConnectionException
    }

    def "calling update operation on connector returning error value" () {
        given: "a connector returning false"
        connector.update(someApiObject) >> failure

        when: "trying to update some Contrail object"
        connection.update(someApiObject)

        then: "it fails with ContrailApiException"
        thrown ConnectionException
    }

    def "calling sync operation on connector returning error value" () {
        given: "a connector returning false"
        connector.sync(someApiUri) >> failure

        when: "trying to sync some Contrail object"
        connection.sync(someApiUri)

        then: "it fails with ContrailApiException"
        thrown ConnectionException
    }

    def "calling findByName on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.findByName(someApiClass, someApiObject, someObjectName) >> { throw new IOException("sorry") }

        when: "trying to find some Contrail object by its name"
        def result = connection.findByName(someApiClass, someApiObject, someObjectName)

        then: "it returns null"
        result == null
    }

    def "calling second version of findByName on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.findByName(someApiClass, someAncestryList) >> { throw new IOException("sorry") }

        when: "trying to find some Contrail object by its name"
        def result = connection.findByName(someApiClass, someAncestryList)

        then: "it returns null"
        result == null
    }

    def "calling find on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.find(someApiClass, someApiObject, someObjectName) >> { throw new IOException("sorry") }

        when: "trying to find some Contrail object"
        def result = connection.find(someApiClass, someApiObject, someObjectName)

        then: "it returns null"
        result == null
    }

    def "calling findById on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.findById(someApiClass, someObjectId) >> { throw new IOException("sorry") }

        when: "trying to find some Contrail object by ID"
        def result = connection.findById(someApiClass, someObjectId)

        then: "it returns null"
        result == null
    }

    def "calling findByFQN on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.findByFQN(someApiClass, someObjectFQN) >> { throw new IOException("sorry") }

        when: "trying to find some Contrail object by its qualified name"
        def result = connection.findByFQN(someApiClass, someObjectFQN)

        then: "it returns null"
        result == null
    }

    def "calling list on a connector throwing IOException results in null return value" () {
        given: "a connector throwing IOException"
        connector.list(someApiClass, null) >> { throw new IOException("sorry") }

        when: "trying to list some Contrail objects"
        def result = connection.list(someApiClass)

        then: "it returns null"
        result == null
    }

    // In this test and the following, connector method's return value
    //     should be set in the THEN clause instead of the GIVEN clause to match correctly.
    //     https://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_combining_mocking_and_stubbing
    def "calling create in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.create(someApiObject)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.create(someApiObject) >> success
    }

    def "calling read in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.read(someApiObject)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.read(someApiObject) >> success
    }

    def "calling update in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.update(someApiObject)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.update(someApiObject) >> success
    }

    def "calling delete in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.delete(someApiObject)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.delete(someApiObject) >> success
    }

    def "calling another version of delete in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.delete(someApiClass, someObjectId)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.delete(someApiClass, someObjectId) >> success
    }

    def "calling sync in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.sync(someApiUri)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.sync(someApiUri) >> success
    }

    def "calling findByName in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.findByName(someApiClass, someApiObject, someObjectName)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.findByName(someApiClass, someApiObject, someObjectName)
    }

    def "calling second version of findByName in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its name"
        connection.findByName(someApiClass, someAncestryList)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.findByName(someApiClass, someAncestryList)
    }

    def "calling find in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object"
        connection.find(someApiClass, someApiObject, someObjectName)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.find(someApiClass, someApiObject, someObjectName)
    }

    def "calling findById in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by ID"
        connection.findById(someApiClass, someObjectId)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.findById(someApiClass, someObjectId)
    }

    def "calling findByFQN in connection calls the underlying connector method" () {
        when: "trying to find some Contrail object by its qualified name"
        connection.findByFQN(someApiClass, someObjectFQN)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.findByFQN(someApiClass, someObjectFQN)
    }

    def "calling list in connection calls the underlying connector method" () {
        when: "trying to list some Contrail objects"
        connection.list(someApiClass)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.list(someApiClass, null)
    }

    def "calling getObjects in connection calls the underlying connector method" () {
        when: "trying to list some Contrail objects"
        connection.getObjects(someApiClass, someReferenceList)

        then: "it calls the correct connector method with the same arguments"
        1 * connector.getObjects(someApiClass, someReferenceList)
    }
}
