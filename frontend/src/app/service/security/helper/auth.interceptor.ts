import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {TokenStorageService} from '../token-storage.service';

const TOKEN_HEADER_KEY = 'Authorization';

/** Hängt das JWT an jede Anfrage an, sobald der Benutzer angemeldet ist. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(TokenStorageService).getToken();
  if (token != null) {
    return next(req.clone({headers: req.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token)}));
  }
  return next(req);
};
