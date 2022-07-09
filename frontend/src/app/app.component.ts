import {Component, ElementRef, HostListener, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TokenStorageService} from "./service/security/token-storage.service";
import {AuswahlService} from "./service/local/auswahl.service";
import { faShoppingCart} from '@fortawesome/free-solid-svg-icons';
import {Offer} from "./service/server/start.service";
import {newArray} from "@angular/compiler/src/util";

export interface Post {
  title: string
  text: string
  id?: number
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  title = 'Hier entsteht Seite mit Angeboten von allen Märkte';
  toggle: boolean = false;
  filmIcon = faShoppingCart;
  ausgewahl: Offer[] = newArray(0);

  constructor( public auth:TokenStorageService, private router:Router,private eRef: ElementRef, public auswahlService: AuswahlService) {
    auswahlService.getValue().subscribe(value => {
      this.ausgewahl = value;
    });
  }

  ngOnInit(): void { }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  openMenu() {
    if ( (window.screen.width <= 682) || (window.innerWidth <= 682) ) {
      this.toggle = !this.toggle;
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event) {

  }
}
