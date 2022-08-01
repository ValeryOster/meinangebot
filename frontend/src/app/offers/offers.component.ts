import {Component, OnInit,} from '@angular/core';
import {Offer, StartService} from "../service/server/start.service";
import {environment} from "../../environments/environment";
import {AuswahlService} from "../service/local/auswahl.service";
import {OfferListService} from "../service/offerslist/offer-list.service";

@Component({
  selector: 'app-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.css']
})

export class OffersComponent implements OnInit {
  discounters: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();

  //for searching presentation
  allOffersList: Array<Offer> = [];

  url = environment.apiUrl;
  ausgewahl: Offer[] = [];
  search = "";

  //diskounter list updating
  discountersList: Array<string> = ['Lidl', 'Penny', 'Aldi', 'Netto'];

  constructor(public service: StartService, public auswahlService: AuswahlService, public offerService: OfferListService) {
    this.search = ""
    auswahlService.ngOnInit();
  }


  ngOnInit(): void {
    this.auswahlService.getValue().subscribe(value => {
      if (value != null && this.ausgewahl.length == 0) {
        return this.ausgewahl = value;
      }
    });

    this.offerService.getDiscounters().subscribe(value => {
      this.service.getSelectedDiskounters(value).subscribe(offers => this.saveItemsToMap(offers))
    });
  }

  saveItemsToMap(value: Object) {
    let strings = Object.keys(value);
    this.discounters.clear();
    this.allOffersList.length = 0;
    for (let i = 0; i < strings.length; i++) {
      let offers = value[strings[i]];
      let map = this.mapToDiscount(offers);
      this.discounters.set(strings[i], map);
      Array.prototype.push.apply(this.allOffersList, offers)
    }
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
      if (this.ausgewahl.findIndex(valM => valM.id === offerNew.id) === -1) {
        this.ausgewahl.push(offerNew);
        this.auswahlService.addValue(this.ausgewahl);
      }
    }
  }
}

