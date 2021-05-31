import {Component, OnInit} from '@angular/core';
import {Offer} from "../service/start.service";
import {OffersComponent} from "../offers/offers.component";
import {environment} from "../../environments/environment";
import {AuswahlService} from "../service/auswahl.service";

@Component({
  selector: 'app-auswahl',
  templateUrl: './auswahl.component.html',
  styleUrls: ['./auswahl.component.css']
})
export class AuswahlComponent implements OnInit {
  auswahlListe: Offer[] = [];
  url = environment.apiUrl;

  constructor(private auswahlService: AuswahlService) {
    auswahlService.getValue().subscribe(value => {
      this.auswahlListe = value;
    });
  }

  loeschen(offer: Offer) {
    this.auswahlService.setValue(this.auswahlListe.filter(value => value.id != offer.id))
  }

  saveInArray(value: Offer) {
    this.auswahlListe.push(value);
  }

  ngOnInit(): void {
  }
}
