import {Injectable, OnInit} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {Offer} from "../server/start.service";
import {SelectedItemsService} from "../server/selected-items.service";
import {TokenStorageService} from "../security/token-storage.service";
import Swal from "sweetalert2";

@Injectable({
  providedIn: 'root'
})
export class AuswahlService  implements OnInit {

  private valueObs: BehaviorSubject<Offer[]> = new BehaviorSubject<Offer[]>(null);

  constructor(private itemsService:SelectedItemsService,public auth:TokenStorageService) {
  }

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.itemsService.getSelectedItems(this.auth.getUser().id).subscribe(value => {
        if (this.valueObs.getValue() != null && this.valueObs.getValue().length > 0) {
          this.addValue(value)
        } else {
          this.setValue(value);
        }
      });

    }
  }

  public setValue(value: Offer[]): void {
    this.valueObs.next(value);
  }

  public getValue(): Observable<Offer[]> {
    return this.valueObs;
  }

  public addValue(value: Offer[]): void {
    let mainValue = this.valueObs.getValue() != null ? this.valueObs.getValue() : [];

    value.forEach(val => {
      if(mainValue.findIndex(valM => valM.id === val.id ) === -1){
        mainValue.push(val);
      }
    });
    this.valueObs.next(mainValue);
  }
  public saveSelectedItems() {
    if (this.auth.isAuthenticated()) {
      this.itemsService.saveSelectedItems(this.valueObs.getValue(), this.auth.getUser().id).subscribe(success => {
        if (success) {
          Swal.fire("Success", "Erfolgreich gespeichert", 'success');
        }
      },error => {
        Swal.fire("Error", error.error.message.replace("Error:", " "), 'error');
      });
    }
  }

  delete(offer: Offer) {
    this.setValue(this.valueObs.getValue().filter(value => value.id != offer.id))
    if (this.auth.isAuthenticated()) {
      let offers =[offer]
      this.itemsService.deleteSelectedItem(offers, this.auth.getUser().id).subscribe();
    }
  }
}
