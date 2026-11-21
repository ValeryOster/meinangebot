import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";

export class Discounter {
  id: number;
  gatherDate: string;
  /** Ergebnis je Discounter, z. B. {"Penny": "120 neue Angebote", "Netto": "FEHLER: ..."} */
  discount: { [name: string]: string };
  duration: number;
  expiryDate: string;
}

export interface GatherStartResponse {
  started: boolean;
  message: string;
  skipped?: string[];
}

export interface GatherStatus {
  running: boolean;
  current: string | null;
  startedAt: string | null;
  available: string[];
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

  startGather(discounters: string[]): Observable<GatherStartResponse> {
    return this.http.post<GatherStartResponse>(this.url + 'gather', discounters);
  }

  getStatus(): Observable<GatherStatus> {
    return this.http.get<GatherStatus>(this.url + 'status');
  }

  delLastInputs(list: any[]) {
    let url = this.url + 'del';
    return this.http.post(url, list);
  }
}
