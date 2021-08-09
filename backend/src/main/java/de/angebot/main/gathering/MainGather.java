package de.angebot.main.gathering;

import de.angebot.main.enities.CommonGather;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.CommonGatherRepo;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@Component
@Configuration
public class MainGather {
    private List<Gathering> gatherList = new ArrayList<>();
    private CommonGatherRepo gatherRepo;


    public void startGather() {
        if (!gatherList.isEmpty()) {
            Long start = System.currentTimeMillis();
            for (Gathering gathering : gatherList) {
                System.out.println("++++++++++++ " + gathering.getDiscountName() + " Started +++++++++++++++");
                gathering.startGathering();
                System.out.println("++++++++++++ " + gathering.getDiscountName() + " Ended   +++++++++++++++");

            }
            long duration = System.currentTimeMillis() - start;
            saveGatheringReport(duration);
            log.info("Gathering is done: " + LocalDate.now() + ", duration: " + duration);
            gatherList.clear();
        }
    }

    public void addToGatherList(Gathering gather) {
        gatherList.add(gather);
    }

    public void deleteFromGatherList(Gathering gather) {
        this.gatherList.remove(gather);
    }

    private Map<String, String> getDiscountMap() {
        return gatherList.stream()
                .collect(Collectors.toMap(Gathering::getDiscountName, Gathering::getDiscountName));
    }

    private void saveGatheringReport(Long duration) {
        CommonGather gathering = new CommonGather();
        gathering.setGatherDate(LocalDate.now());
        gathering.setDiscount(getDiscountMap());
        gathering.setDuration(duration);
        gathering.setExpiryDate(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
        gatherRepo.save(gathering);

    }

    public void setGatherRepo(CommonGatherRepo gatherRepo) {
        this.gatherRepo = gatherRepo;
    }
}
