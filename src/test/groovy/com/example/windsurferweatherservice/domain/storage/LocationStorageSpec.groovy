package com.example.windsurferweatherservice.domain.storage

import com.example.windsurferweatherservice.application.adviser.exception.NoAvailableLocationsException
import com.example.windsurferweatherservice.domain.model.Location
import com.example.windsurferweatherservice.shared.loader.LocationsDataLoader
import spock.lang.Specification

class LocationStorageSpec extends Specification {

    def dataLoader = Mock(LocationsDataLoader)
    def storage = new LocationStorage(dataLoader)


    def "should load and return all locations on application started event"() {
        given:
        def locations = [
                new Location("Jastarnia", "Poland", 54.7, 18.67),
                new Location("Bridgetown", "Barbados", 13.1, -59.6),
                new Location("Fortaleza", "Brazil", -3.7, -38.5)
        ]

        and:
        storage.locations.addAll(locations)

        when:
        def result = storage.findAll()


        then:
        result.size() == 3
        result.eachWithIndex { location, i ->
            assert location.name() == locations[i].name()
            assert location.country() == locations[i].country()
            assert location.latitude() == locations[i].latitude()
            assert location.longitude() == locations[i].longitude()
        }
    }


    def "should throw exception when loaded locations list is empty"() {
        given:
        storage.locations.clear()

        when:
        storage.findAll()

        then:
        def exception = thrown(NoAvailableLocationsException)
        exception.message == "No locations configured in the system"
    }
}
