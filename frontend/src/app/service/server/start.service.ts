
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
}

@Injectable({
  providedIn: 'root'
})
export class StartService {
  private url = environment.apiUrl+'/home/';

  constructor(private http: HttpClient) {
  }

  getPenny(): Observable<Offer[]> {
    let url = this.url + 'penny';

    return this.http.get<Offer[]>(url)
  }

  getPennyById(id: any): Observable<Offer> {
    let url = this.url + 'penny/'+id;
    return this.http.get<Offer>(url);
  }

  getLidlById(id: any): Observable<Offer> {
    let url = this.url + 'lidl/'+id;
    return this.http.get<Offer>(url);
  }

  getAldiById(id: any): Observable<Offer> {
    let url = this.url + 'aldi/${id}';
    return this.http.get<Offer>(url);
  }

  getLidl(): Observable<Offer[]> {
    let url = this.url + 'lidl';
    return this.http.get<Offer[]>(url);
  }

  getSelectedDiskounters(string:any): Observable<Offer[]> {
    let url = this.url + 'auswahl';
    return this.http.post<Offer[]>(url, string);
  }
}
