package pl.api.itoffers.provider.justjoinit.ui.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import pl.api.itoffers.offer.ui.cli.CliFixParams;
import pl.api.itoffers.provider.justjoinit.JustJoinItRepository;
import pl.api.itoffers.provider.justjoinit.model.JustJoinItDateTime;
import pl.api.itoffers.provider.justjoinit.model.JustJoinItRawOffer;
import pl.api.itoffers.provider.justjoinit.service.JustJoinItPayloadExtractor;
import pl.api.itoffers.shared.aws.AwsS3Connector;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * OneUse - remove after usage
 * @deprecated
 */
@Slf4j
@ShellComponent
@Transactional
@AllArgsConstructor
public class ImportJJITOffersFromFileCli {
    // todo create next cli to migrate JJIT MongoDB Offers to Postgres Offer
    // todo once it will be functionally finished - refactor all/create separated services to fetch JSON file
    private static final String FILE_NAME = "dev-recruit.just_join_it_raw_offers.json";

    private AwsS3Connector awsS3Connector;
    private ObjectMapper mapper;
    private JustJoinItRepository repository;
    private JustJoinItPayloadExtractor extractor;

    @ShellMethod(key="jjit-s3")
    public void saveInMongoDBJJITOffersStoredInS3(
        @ShellOption(defaultValue = "test") String mode,
        @ShellOption(defaultValue = "1") String limit
    ) throws IOException {

        CliFixParams params = new CliFixParams(mode, Integer.valueOf(limit));

        log.info("Start fetching {}", FILE_NAME);
        String justJoinItOffers = awsS3Connector.fetchJson(FILE_NAME);
        log.info("Fetched");
        ArrayList<Map<String, Object>> rawOffers = extractor.convert(mapper.readTree(justJoinItOffers).elements());
        UUID scrappingId = UUID.randomUUID();
        log.info("ScrappingId: {}", scrappingId);

        Map<String, List<JustJoinItRawOffer>> jjitOffers = new HashMap<>();

        for (Map<String, Object> rawOffer : rawOffers) {
            Map<String, Object> offerMetadata = (Map<String, Object>) rawOffer.get("offer");
            String slug = (String) offerMetadata.get("slug");
            List<JustJoinItRawOffer> alreadySavedSameOffers = jjitOffers.get(slug);

            if (null == alreadySavedSameOffers) {
                List<JustJoinItRawOffer> jjitOffersCollection = new ArrayList<>();
                jjitOffers.put(slug, jjitOffersCollection);
            } else {
                alreadySavedSameOffers.add(createJJITDocument(scrappingId, rawOffer));
            }
        }

        if (! params.isMigration()) {
            // todo show report
            return;
        }

        // todo add some index to use limit of saving
        // todo check in MongoDB it is not already added

//        offers.forEach(offer -> {
////            repository.save(
////                new JustJoinItRawOffer(
////                    scrappingId,
////                    (String) offer.get("technology"),
////                    offer,
////                    JustJoinItDateTime.createFrom(rawCreatedAt.get("$date")).value
////                )
////            );
//        });
    }

    public static JustJoinItRawOffer createJJITDocument(UUID scrappingId, Map<String, Object> rawOffer) {
        LinkedHashMap<String, String> rawCreatedAt = (LinkedHashMap<String, String>) rawOffer.get("createdAt");
        Map<String, Object> offerMetadata = (Map<String, Object>) rawOffer.get("offer");

        return new JustJoinItRawOffer(
            scrappingId,
            (String) rawOffer.get("technology"),
            offerMetadata,
            JustJoinItDateTime.createFrom(rawCreatedAt.get("$date")).value
        );
    }
}
