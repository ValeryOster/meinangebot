package de.angebot.main.services;

import de.angebot.main.enities.services.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.aldi.AldiOffer;
import de.angebot.main.gathering.edeka.EdekaOffer;
import de.angebot.main.gathering.lidl.LidlOffer;
import de.angebot.main.gathering.netto.NettoOffer;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.discounters.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatherService {
    @Autowired
    private MainGather mainGather;
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
    @Autowired
    private NettoOffer nettoOffer;
    @Autowired
    private NettoRepo nettoRepo;
    @Autowired
    private EdekaOffer edekaOffer;
    @Autowired
    private EdekaRepo edekaRepo;
    @Autowired
    private GlobusRepo globusRepo;

    public void startGather(List<String> discounters) {
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
                case "NETTO":
                    nettoRepo.deleteAllActuel();
                    break;
                case "EDEKA":
                    edekaRepo.deleteAllActuel();
                    break;
                case "GLOBUS":
                    globusRepo.deleteAllActuel();
                    break;
            }
        });
    }
}
