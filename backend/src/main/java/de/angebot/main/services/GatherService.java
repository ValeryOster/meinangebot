package de.angebot.main.services;

import de.angebot.main.enities.services.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.aldi.AldiOffer;
import de.angebot.main.gathering.edeka.EdekaOffer;
import de.angebot.main.gathering.lidl.LidlOffer;
import de.angebot.main.gathering.netto.NettoOffer;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.discounters.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GatherService {

    private final MainGather mainGather;
    private final PennyOffer pennyOffer;
    private final LidlOffer lidlOffer;
    private final AldiOffer aldiOffer;
    private final NettoOffer nettoOffer;
    private final EdekaOffer edekaOffer;
    private final LidlRepo lidlRepo;
    private final PennyRepo pennyRepo;
    private final AldiRepo aldiRepo;
    private final NettoRepo nettoRepo;
    private final EdekaRepo edekaRepo;

    public GatherService(MainGather mainGather,
                         PennyOffer pennyOffer,
                         LidlOffer lidlOffer,
                         AldiOffer aldiOffer,
                         NettoOffer nettoOffer,
                         EdekaOffer edekaOffer,
                         LidlRepo lidlRepo,
                         PennyRepo pennyRepo,
                         AldiRepo aldiRepo,
                         NettoRepo nettoRepo,
                         EdekaRepo edekaRepo) {
        this.mainGather = mainGather;
        this.pennyOffer = pennyOffer;
        this.lidlOffer = lidlOffer;
        this.aldiOffer = aldiOffer;
        this.nettoOffer = nettoOffer;
        this.edekaOffer = edekaOffer;
        this.lidlRepo = lidlRepo;
        this.pennyRepo = pennyRepo;
        this.aldiRepo = aldiRepo;
        this.nettoRepo = nettoRepo;
        this.edekaRepo = edekaRepo;
    }

    public void startGather(List<String> discounters) {
        // TODO: 12.03.2021 Umbauen -> Enum mit Autowired anstatt for
        for (String discounter : discounters) {
            switch (discounter.toUpperCase(Locale.ROOT)) {
                case "LIDL":
                    mainGather.addToGatherList(lidlOffer);
                    break;
                case "PENNY":
                    mainGather.addToGatherList(pennyOffer);
                    break;
                case "ALDI":
                    mainGather.addToGatherList(aldiOffer);
                    break;
                case "NETTO":
                    mainGather.addToGatherList(nettoOffer);
                    break;
                case "EDEKA":
                    mainGather.addToGatherList(edekaOffer);
                    break;
            }
        }
        mainGather.startGather();
    }

    public List<CommonGather> findAll() {
        return mainGather.getGatherReport();
    }

    public void deleteLastInputs(List<String> discounters) {
        discounters.forEach(discounter -> {
            switch (discounter.toUpperCase(Locale.ROOT)) {
                case "LIDL":
                    lidlRepo.deleteAllActuel();
                    break;
                case "PENNY":
                    pennyRepo.deleteAllActuel();
                    break;
                case "ALDI":
                    aldiRepo.deleteAllActuel();
                    break;
                case "NETTO":
                    nettoRepo.deleteAllActuel();
                    break;
                case "EDEKA":
                    edekaRepo.deleteAllActuel();
                    break;
            }
        });
    }
}
