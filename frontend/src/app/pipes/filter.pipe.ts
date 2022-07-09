import { Pipe, PipeTransform } from '@angular/core';
import {Offer} from "../service/server/start.service";

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {

  transform(discounters:Array<Offer>, search: string = ''):any {
    // console.log(discounters)
    if (!search.trim()) {
      return discounters;
    }

    let offers = discounters.filter(value => value.produktName.toLowerCase().includes(search.toLowerCase()));

    return offers;
  }

}
