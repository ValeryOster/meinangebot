import { Injectable } from '@angular/core';
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Offer} from "./start.service";

@Injectable({
  providedIn: 'root'
})
export class SelectedItemsService {

  private url = environment.apiUrl+'/selected/';

  constructor(private http: HttpClient) {
  }

  getSelectedItems(userId:any): Observable<Offer[]> {
    let url = this.url + 'itmes';
    return this.http.post<Offer[]>(url, userId);
  }

  saveSelectedItems(auswahlListe: Offer[], userId:any) {
    let url = this.url + 'save';
    let body = {auswahlListe, userId};
    return this.http.post<any>(url, body);
  }

  deleteSelectedItem(auswahlListe: Offer[], userId:any) {
    let url = this.url + 'delete';
    let body = {auswahlListe, userId};
    return this.http.post<any>(url, body);
  }
}
