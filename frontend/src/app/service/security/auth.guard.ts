import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {Observable} from "rxjs";
import {TokenStorageService} from "./token-storage.service";


@Injectable({providedIn: 'root'})
export class AuthGuard implements CanActivate {
  constructor(private authService: TokenStorageService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    if (this.authService.isAuthenticated()) {
      const roles = this.authService.getRoles();
      if (roles != null && roles.indexOf("ROLE_ADMIN") >= 0) {
        return true;
      }
    }
    return this.router.createUrlTree(['/'], {queryParams: {auth: false}});
  }
}
