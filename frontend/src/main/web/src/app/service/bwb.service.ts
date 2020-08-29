import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export class Bewerbung {
  id: number;
  userId: number;
  firmName: string;
  firmAdress: string;
  ansprechPartner: string;
  telNummer: string;
  zustand: string;
  notizen: string;
  bewerbungsDatum: string;
  email: string;
}

@Injectable({providedIn: 'root'})
export class BwbService {
  private url = 'http://localhost:8080/';

  constructor(private http: HttpClient) {
  }

  getTitle(): Observable<string> {
    return this.http.get<string>(this.url + 'home');
  }


}
