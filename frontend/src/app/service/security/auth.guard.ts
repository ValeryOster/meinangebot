import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {TokenStorageService} from "./token-storage.service";


@Injectable({providedIn: 'root'})
export class AuthGuard implements CanActivate {
  constructor(private authService: TokenStorageService,
              private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean
  {
    if (this.authService.isAuthenticated()) {
      return true;
    } else {
      this.router.navigate(['/'], {
        queryParams: {auth: false}
      })
    }
  }
}
