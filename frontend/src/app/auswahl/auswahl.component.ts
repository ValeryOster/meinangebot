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
  selectedList: Offer[] = [];
  url = environment.apiUrl;

  constructor(private selectedService: AuswahlService , public auth: TokenStorageService) {

    selectedService.getValue().subscribe(value => {
      this.selectedList = value;
      console.log(value);
    });
  }

  delete(offer: Offer) {
    this.selectedService.delete(offer)
  }

  saveInArray(value: Offer) {
    this.selectedList.push(value);
  }

  save() {
    this.selectedService.saveSelectedItems();
  }

  getColor(offer: Offer) {
    console.log(offer);
    return undefined;
  }
}
