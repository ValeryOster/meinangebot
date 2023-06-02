package de.angebot.main.schedules;

import de.angebot.main.config.Discounters;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.common.Gathering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GatheringSchedule {
    private final MainGather mainGather;
    private final Discounters discounters;
    @Autowired
    public GatheringSchedule(MainGather mainGather, Discounters discounters) {
        this.mainGather = mainGather;
        this.discounters = discounters;
    }
    @Scheduled(cron = "0 0 3 * * SUN", zone = "Europe/Berlin")
    public void gatherOnTime() {
        for (Gathering discounter : discounters.getAllDiscountersGather()) {
            try {
                mainGather.addToGatherList(discounter);
            } catch (Exception e) {
               log.error("Enum->Discounters can't instantiated");
            }
        }
        mainGather.startGather();
    }
}
