import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Offer} from './start.service';

/** Gespeicherte Auswahl des angemeldeten Benutzers (der Server bestimmt den Benutzer aus dem JWT). */
@Injectable({
  providedIn: 'root'
})
export class SelectedItemsService {
  private url = environment.apiUrl + '/selected/';

  constructor(private http: HttpClient) {
  }

  getSelectedItems(): Observable<Offer[]> {
    return this.http.get<Offer[]>(this.url + 'items');
  }

  saveSelectedItems(auswahlListe: Offer[]): Observable<boolean> {
    return this.http.post<boolean>(this.url + 'save', {auswahlListe});
  }

  deleteSelectedItem(auswahlListe: Offer[]): Observable<void> {
    return this.http.post<void>(this.url + 'delete', {auswahlListe});
  }
}
