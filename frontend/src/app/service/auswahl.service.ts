import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {Offer} from "./start.service";

@Injectable({
  providedIn: 'root'
})
export class AuswahlService {

  constructor() { }

  private valueObs: BehaviorSubject<Offer[]> = new BehaviorSubject<Offer[]>(null);

  public setValue(value: Offer[]):void {
    this.valueObs.next(value);
  }

  public getValue():Observable<Offer[]> {
    return this.valueObs;
  }

}
