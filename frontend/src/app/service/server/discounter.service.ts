import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";

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

  private url = environment.apiUrl+'/manage/';

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

  delLastInputs(list: any[]) {
    let url = this.url + 'del';
    return this.http.post(url, list);
  }
}
