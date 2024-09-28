package pl.api.itoffers.offer.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.api.itoffers.offer.application.factory.OfferFactory;
import pl.api.itoffers.offer.application.factory.SalariesFactory;
import pl.api.itoffers.offer.application.repository.CategoryRepository;
import pl.api.itoffers.offer.application.repository.CompanyRepository;
import pl.api.itoffers.offer.application.repository.OfferRepository;
import pl.api.itoffers.offer.application.repository.SalaryRepository;
import pl.api.itoffers.offer.domain.*;
import pl.api.itoffers.provider.justjoinit.JustJoinItRepository;
import pl.api.itoffers.provider.justjoinit.model.JustJoinItRawOffer;

import java.util.*;

@Slf4j
@Service
public class OfferService {

    @Autowired
    private JustJoinItRepository jjitRawOffersRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private OfferFactory offerFactory;
    @Autowired
    private SalariesFactory salariesFactory;
    @Autowired
    private SalaryRepository salaryRepository;

    public void processOffersFromExternalService(UUID scrappingId)
    {
        List<JustJoinItRawOffer> rawOffers = jjitRawOffersRepository.findByScrapingId(scrappingId);

        for (JustJoinItRawOffer rawOffer : rawOffers) {

            Map<String, Set<Category>> categories = offerFactory.createCategories(rawOffer);
            Company company = offerFactory.createCompany(rawOffer);
            Offer offer = offerFactory.createOffer(rawOffer, categories.get("forEntity"), company);

            Offer alreadyStoredOffer = findAlreadyStoredOffer(offer);

            if (null != alreadyStoredOffer) {
                continue;
            }

            // todo add transaction for all that saving
            companyRepository.save(company);
            categoryRepository.saveAll(categories.get("toSave"));
            offerRepository.save(offer);
            salaryRepository.saveAll(salariesFactory.create(offer, rawOffer));
            log.info(String.format("[just-join-it][migration] new offer %s", offer));
        }
    }

    private Offer findAlreadyStoredOffer(Offer offer) {
        return offerRepository.findByDifferentOffer(
                offer.getSlug(),
                offer.getTitle(),
                offer.getCompany().getName(),
                offer.getPublishedAt()
        );
    }
}
