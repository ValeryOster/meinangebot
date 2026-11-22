
import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";

export class Offer {
  id: number;
  produktName: string;
  produktMaker: string;
  produktPrise: string;
  produktRegularPrise: string;
  produktDescription: string;
  imageLink: string;
  vonDate: string;
  bisDate: string;
  discounterName: string;
  kategorie: string;
  url: string;
  discounterLogo: string;
  isSaved:boolean
}

@Injectable({
  providedIn: 'root'
})
export class StartService {
  private url = environment.apiUrl + '/home/';

  constructor(private http: HttpClient) {
  }

  /** Aktuelle Angebote der gewählten Discounter, gruppiert nach Discounter ("Penny" → Angebote). */
  getSelectedDiskounters(discounters: string[]): Observable<{ [discounter: string]: Offer[] }> {
    return this.http.post<{ [discounter: string]: Offer[] }>(this.url + 'auswahl', discounters);
  }
}
