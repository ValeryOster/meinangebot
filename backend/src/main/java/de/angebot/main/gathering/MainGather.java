package de.angebot.main.gathering;

import de.angebot.main.gathering.common.Gathering;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MainGather {
    private List<Gathering> gatherList = new ArrayList<>();

    public void startGather() {
        gatherList.forEach(gathering -> gathering.startGathering());
    }

    public void addToGatherList(Gathering gather) {
        gatherList.add(gather);
    }

    public void deleteFromGatherList(Gathering gather) {
        this.gatherList .remove(gather);
    }

    public Map<String, Boolean> getDiscountMap() {
        return gatherList.stream()
                .collect(Collectors.toMap(gathering -> gathering.getDiscountName(), o -> true));
    }
}
