import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {StartService} from "../server/start.service";

@Injectable({
  providedIn: 'root'
})
export class OfferListService{
  private discounters: BehaviorSubject<Array<string>> = new BehaviorSubject<Array<string>>(['Lidl','Penny','Aldi','Netto', 'Edeka']);

  constructor(public service: StartService) {}


  public getDiscounters(): Observable<Array<string>> {
    return this.discounters;
  }

  public setDiscounters(value:any) {
    this.discounters.next(value);
  }
}
