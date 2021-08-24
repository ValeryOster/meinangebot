import {Component, OnInit,} from '@angular/core';
import {Offer, StartService} from "../service/start.service";
import {environment} from "../../environments/environment";
import {AuswahlService} from "../service/auswahl.service";

@Component({
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.css']
})
export class OffersComponent implements OnInit {
  discounters: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();
  url = environment.apiUrl;
  ausgewahl: Offer[] = [];

  constructor(public service: StartService, public auswahlService: AuswahlService) {}

  ngOnInit(): void {
    this.service.getAll().subscribe(value => {
      let strings = Object.keys(value);
      for (let i = 0; i < strings.length; i++) {
        let map = this.mapToDiscount(value[strings[i]]);
        this.discounters.set(strings[i], map);
      }
    })
    this.auswahlService.getValue().subscribe(value => {
      if (value != null && this.ausgewahl.length == 0) {
        return this.ausgewahl = value;
      }
    });
  }

  mapToDiscount(offers: Offer[]) {
    let discounterMap = new Map();
    offers = offers.sort((a, b) => (a.kategorie < b.kategorie ? -1 : 1))

    let kat = offers.map(value1 => value1.kategorie).filter(
      (value1, index, array) => array.indexOf(value1) == index
    );

    kat.forEach(kategori => {
      let offers1 = offers.filter(value1 => value1.kategorie == kategori);
      discounterMap.set(kategori, offers1);
    })

    return discounterMap;
  }

  addToBucket(offerNew: Offer) {
    if (this.ausgewahl.indexOf(offerNew) === -1) {
      this.ausgewahl.push(offerNew);
      this.auswahlService.setValue(this.ausgewahl);
    }
  }
}
