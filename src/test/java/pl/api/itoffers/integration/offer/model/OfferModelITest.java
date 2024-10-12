package pl.api.itoffers.integration.offer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.api.itoffers.helper.AbstractITest;
import pl.api.itoffers.helper.OfferBuilder;
import pl.api.itoffers.integration.offer.helper.OfferTestManager;
import pl.api.itoffers.offer.application.repository.OfferRepository;
import pl.api.itoffers.offer.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferModelITest extends AbstractITest {

    @Autowired
    private OfferTestManager offerTestManager;
    @Autowired
    private OfferRepository offerRepository;
    private OfferBuilder offerBuilder;

    @BeforeEach
    public void setUp() {
        super.setUp();
        this.offerBuilder = offerTestManager.createOfferBuilder();
        offerTestManager.clearAll();
    }

    @Test
    public void shouldSaveOfferEntity() {
        Offer offer = offerBuilder.plainJob("php").saveAndGetOffer();

        Optional<Offer> fetchedOffer = offerRepository.findById(offer.getId());
        assertThat(fetchedOffer.get().getCreatedAt()).isNotNull();
        assertThat(fetchedOffer.get().getSalaries()).isEmpty();
    }
}
