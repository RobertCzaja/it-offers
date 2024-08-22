package pl.api.itoffers.integration.provider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.api.itoffers.helper.AbstractITest;
import pl.api.itoffers.provider.Offer;
import pl.api.itoffers.provider.infrastructure.OfferRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO to remove when real business cases with MongoDB show up
 */
public class MongoDBConnectionITest extends AbstractITest {

    @Autowired
    private OfferRepository repository;

    @Test
    void shouldCorrectlySaveDataToMongoDB() {
        UUID id = UUID.randomUUID();
        Offer offer = new Offer(id.toString(), "integration test");

        repository.save(offer);

        Optional<Offer> foundOffer = repository.findById(id.toString());
        assertThat(foundOffer.get().getTile()).isEqualTo("integration test");
    }
}