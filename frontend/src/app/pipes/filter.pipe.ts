import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {

  transform(discounters: Map<string, string>, search: string = ''):Map<string, string> {
    // console.log(discounters)
    if (!search.trim()) {
      return discounters;
    }
    discounters.forEach((value, key) => console.log(value));
    return discounters.values();
  }

}
