import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {Offer, StartService} from "../service/server/start.service";
import {environment} from "../../environments/environment";
import {AuswahlService} from "../service/local/auswahl.service";
import {OfferListService} from "../service/offerslist/offer-list.service";
import {DatePipe, KeyValuePipe, NgTemplateOutlet} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {FilterPipe} from '../pipes/filter.pipe';

@Component({
  selector: 'app-offers',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [DatePipe, KeyValuePipe, NgTemplateOutlet, FormsModule, MatTabsModule, MatCardModule, MatIconModule, FilterPipe],
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.css']
})

export class OffersComponent implements OnInit {
  discounters: Map<string, Map<string, Offer[]>> = new Map<string, Map<string, Offer[]>>();

  //for searching presentation
  allOffersList: Array<Offer> = [];

  url = environment.apiUrl;
  ausgewahl: Offer[] = [];
  search = "";

  constructor(public service: StartService, public auswahlService: AuswahlService, public offerService: OfferListService) {
    this.search = ""
    auswahlService.init();
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

  saveItemsToMap(value: { [discounter: string]: Offer[] }) {

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

  mapToDiscount(offers: Offer[]): Map<string, Offer[]> {
    let discounterMap = new Map<string, Offer[]>();
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

  /** Logo-Datei je Discounter (für die Suchergebnisse); null = Textlogo */
  logoFor(offer: Offer): string | null {
    const name = (offer.discounterName || '').toLowerCase();
    if (name.includes('penny')) return 'assets/penny.png';
    if (name.includes('netto')) return 'assets/netto.svg';
    if (name.includes('edeka')) return 'assets/edeka.svg';
    return null;
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

