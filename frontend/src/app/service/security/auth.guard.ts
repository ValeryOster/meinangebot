import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {TokenStorageService} from "./token-storage.service";


@Injectable({providedIn: 'root'})
export class AuthGuard implements CanActivate {
  constructor(private authService: TokenStorageService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean
  {
    function notPermitted() {
      this.router.navigate(['/'], {
        queryParams: {auth: false}
      })
    }

    if (this.authService.isAuthenticated()) {
      let roles = this.authService.getRoles();
      if (roles != null && roles.indexOf("ROLE_ADMIN") >= 0) {
        return true;
      } else {
        notPermitted.call(this);
      }
    } else {
      notPermitted.call(this);
    }
  }
}
