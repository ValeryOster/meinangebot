import {Component, ElementRef, HostListener, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TokenStorageService} from "./service/security/token-storage.service";
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

  constructor( public auth:TokenStorageService, private router:Router,private eRef: ElementRef) {
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
