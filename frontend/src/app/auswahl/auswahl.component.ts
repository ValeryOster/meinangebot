import {Component} from '@angular/core';
import {Offer} from "../service/server/start.service";
import {environment} from "../../environments/environment";
import {AuswahlService} from "../service/local/auswahl.service";
import {TokenStorageService} from "../service/security/token-storage.service";

@Component({
  selector: 'app-auswahl',
  templateUrl: './auswahl.component.html',
  styleUrls: ['./auswahl.component.css']
})
export class AuswahlComponent {
  auswahlListe: Offer[] = [];
  url = environment.apiUrl;

  constructor(private auswahlService: AuswahlService , public auth: TokenStorageService) {
    auswahlService.getValue().subscribe(value => {
      this.auswahlListe = value;
    });
  }

  loeschen(offer: Offer) {
    this.auswahlService.delete(offer)
  }

  saveInArray(value: Offer) {
    this.auswahlListe.push(value);
  }

  save() {
    this.auswahlService.saveSelectedItems();
  }
}
