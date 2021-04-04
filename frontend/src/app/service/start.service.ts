import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

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
  private url = 'http://localhost:8080/home/';

  constructor(private http: HttpClient) {
  }

  getPenny(): Observable<Offer[]> {
    let url = this.url + 'penny';

    return this.http.get<Offer[]>(url)
  }

  getLidl(): Observable<Offer[]> {
    let url = this.url + 'lidl';

    return this.http.get<Offer[]>(url)
  }

  getAll(): Observable<Offer[]> {
    let url = this.url + 'all';

    return this.http.get<Offer[]>(url)
  }
}
