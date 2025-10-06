package com.example.windsurferweatherservice.domain.storage;

import com.example.windsurferweatherservice.application.adviser.exception.NoAvailableLocationsException;
import com.example.windsurferweatherservice.shared.loader.LocationsDataLoader;
import com.example.windsurferweatherservice.domain.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationStorage {

    private final LocationsDataLoader dataLoader;
    private static final List<Location> locations = new CopyOnWriteArrayList<>();

    @EventListener
    public void handleApplicationStartingEvent(ApplicationStartedEvent ignored) {
        log.info("Application is started, loading cached positions.");
        dataLoader.load()
                .getLocations()
                .forEach(it -> {
                    var location = new Location(it.getName(), it.getCountry(), it.getLatitude(), it.getLongitude());
                    locations.add(location);
                });
    }


    public List<Location> findAll() {
        if (locations.isEmpty())
            throw new NoAvailableLocationsException("No locations configured in the system");
        return locations;
    }
}
