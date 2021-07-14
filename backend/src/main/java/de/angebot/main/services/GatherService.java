package de.angebot.main.services;

import de.angebot.main.enities.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.aldi.AldiOffer;
import de.angebot.main.gathering.lidl.LidlOffer;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.AldiRepo;
import de.angebot.main.repositories.CommonGatherRepo;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatherService {
    @Autowired
    private MainGather mainGather;

    @Autowired
    private CommonGatherRepo gatherRepo;

    @Autowired
    private PennyOffer pennyOffer;

    @Autowired
    private LidlOffer lidlOffer;

    @Autowired
    private AldiOffer aldiOffer;

    @Autowired
    private LidlRepo lidlRepo;

    @Autowired
    private PennyRepo pennyRepo;

    @Autowired
    private AldiRepo aldiRepo;

    public void startGather(List<String> discounters) {
        mainGather.setGatherRepo(gatherRepo);

        // TODO: 12.03.2021 Umbauen -> Enum mit Autowired anstatt for
        for (String discounter : discounters) {
            switch (discounter) {
                case "LIDL":
                    mainGather.addToGatherList(lidlOffer);
                    break;
                case "PENNY":
                    mainGather.addToGatherList(pennyOffer);
                    break;
                case "ALDI":
                    mainGather.addToGatherList(aldiOffer);
                break;
            }
        }
        mainGather.startGather();
    }

    public List<CommonGather> findAll() {
        return gatherRepo.findAll();
    }

    public void deleteLastInputs(List<String> discounters) {
        discounters.forEach(discounter -> {
            switch (discounter) {
                case "LIDL":
                    lidlRepo.deleteAllActuel();
                    break;
                case "PENNY":
                    pennyRepo.deleteAllActuel();
                    break;
                case "ALDI":
                    aldiRepo.deleteAllActuel();
                    break;
            }
        });
    }
}
