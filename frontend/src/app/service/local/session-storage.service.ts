import { Injectable } from '@angular/core';
import {Offer} from "../server/start.service";

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {
  private  key = "selectedItems";

  constructor() {}

  public getItems(): Offer[]  {
    return JSON.parse(window.sessionStorage.getItem(this.key));
  }
  public setItems(value: Offer[] ) {
    if (value !== null) {

      let mainValue: Offer[]  = JSON.parse(window.sessionStorage.getItem(this.key));
      if (mainValue != null) {
        value.forEach(val => {
          if(mainValue.findIndex(valM => valM.id === val.id ) === -1){
            mainValue.push(val);
          }
        });
        window.sessionStorage.setItem(this.key, JSON.stringify(mainValue));
      }else {
        window.sessionStorage.setItem(this.key, JSON.stringify(value));
      }
    }
  }
}
