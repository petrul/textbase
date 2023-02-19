import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminInfo } from '../dto/AdminInfo';
import { RestApiClientService } from '../rest-api-client.service';

@Component({
  selector: 'app-server-information',
  templateUrl: './server-information.page.html',
  styleUrls: ['./server-information.page.scss'],
})
export class ServerInformationPage implements OnInit {

  constructor(private activatedRoute: ActivatedRoute,
    private api: RestApiClientService) { }

  info: AdminInfo;

  async ngOnInit() {
    // this.folder = this.activatedRoute.snapshot.paramMap.get('id');

    this.info = await this.api.getVersion();
  }

}
