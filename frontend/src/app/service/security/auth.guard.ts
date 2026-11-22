import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {TokenStorageService} from './token-storage.service';

/** Lässt nur Benutzer mit der Rolle ROLE_ADMIN auf die Verwaltungsseite. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(TokenStorageService);
  if (auth.isAuthenticated()) {
    const roles = auth.getRoles();
    if (roles != null && roles.indexOf('ROLE_ADMIN') >= 0) {
      return true;
    }
  }
  return inject(Router).createUrlTree(['/'], {queryParams: {auth: false}});
};
