import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export class Discounter {
  id: number;
  gatherDate: string;
  discount: {
    Penny: string;
    Netto: string;
    Lidl: string;
  };
  duration: string;
  expiryDate: string;

  getDate():string{
    return this.expiryDate;
  }
}

@Injectable({
  providedIn: 'root'
})
export class DiscounterService {

  private url = 'http://localhost:8080/manage/';

  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Discounter[]> {
    let url = this.url + 'start';

    return this.http.get<Discounter[]>(url)
  }

  startGather(selectedOrderIds: any[]) {
    let url = this.url + 'gather';
    return this.http.post(url, selectedOrderIds);
  }

}
