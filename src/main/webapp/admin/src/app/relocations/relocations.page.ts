import { Component, OnInit } from '@angular/core';
import { RestApiClientService } from '../rest-api-client.service';

@Component({
  selector: 'app-relocations',
  templateUrl: './relocations.page.html',
  styleUrls: ['./relocations.page.scss'],
})
export class RelocationsPage implements OnInit {

  constructor(private api: RestApiClientService) { }

  relocations: any; // = [];

  async ngOnInit() {
    this.relocations = await this.api.getDataRestRelocations();
  }

  async delete(r) {
  }

}
