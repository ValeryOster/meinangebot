import { Injectable } from '@angular/core';
import {Offer} from "../server/start.service";

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {
  private  key = "selectedItems";

  constructor() {}

  public getItems(): Offer[]  {
    return JSON.parse(window.sessionStorage.getItem(this.key));
  }

  /** Replaces the stored list with the given value (no merge). */
  public replaceItems(value: Offer[]): void {
    if (value !== null) {
      window.sessionStorage.setItem(this.key, JSON.stringify(value));
    }
  }

  /** Merges new items into the existing stored list (adds only if not already present by id). */
  public mergeItems(value: Offer[]): void {
    if (value === null) {
      return;
    }
    const existing: Offer[] = JSON.parse(window.sessionStorage.getItem(this.key));
    if (existing != null) {
      value.forEach(val => {
        if (existing.findIndex(valM => valM.id === val.id) === -1) {
          existing.push(val);
        }
      });
      window.sessionStorage.setItem(this.key, JSON.stringify(existing));
    } else {
      window.sessionStorage.setItem(this.key, JSON.stringify(value));
    }
  }

  /** @deprecated Use replaceItems or mergeItems explicitly. */
  public setItems(value: Offer[]): void {
    this.mergeItems(value);
  }
}
